import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { SecurityEvent } from '../types/event';
import { Threat } from '../types/threat';

export type ConnectionState = 'CONNECTED' | 'DISCONNECTED' | 'CONNECTING' | 'ERROR';

export type EventCallback = (event: SecurityEvent) => void;
export type ThreatCallback = (threat: Threat) => void;
export type ThreatUpdateCallback = (threat: Threat) => void;
export type AppEventCallback = (event: SecurityEvent) => void;
export type ConnectionStateCallback = (state: ConnectionState) => void;

class SecuritySocketManager {
  private client: Client | null = null;
  private connectionState: ConnectionState = 'DISCONNECTED';
  private connectionStateListeners: Set<ConnectionStateCallback> = new Set();
  
  private eventListeners: Set<EventCallback> = new Set();
  private threatListeners: Set<ThreatCallback> = new Set();
  private threatUpdateListeners: Set<ThreatUpdateCallback> = new Set();
  private appListeners: Map<string, Set<AppEventCallback>> = new Map();

  private activeSubscriptions: Map<string, StompSubscription> = new Map();

  public connect(): void {
    if (this.client && this.client.active) {
      return;
    }

    this.setConnectionState('CONNECTING');

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws/security`;

    this.client = new Client({
      brokerURL: wsUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        this.setConnectionState('CONNECTED');
        this.subscribeTopics();
      },

      onDisconnect: () => {
        this.setConnectionState('DISCONNECTED');
        this.clearSubscriptions();
      },

      onStompError: (frame) => {
        console.error('STOMP Error:', frame.headers['message'], frame.body);
        this.setConnectionState('ERROR');
      },

      onWebSocketClose: () => {
        this.setConnectionState('DISCONNECTED');
        this.clearSubscriptions();
      },
    });

    this.client.activate();
  }

  public disconnect(): void {
    if (this.client) {
      this.clearSubscriptions();
      this.client.deactivate();
      this.client = null;
      this.setConnectionState('DISCONNECTED');
    }
  }

  public getConnectionState(): ConnectionState {
    return this.connectionState;
  }

  public onConnectionStateChange(listener: ConnectionStateCallback): () => void {
    this.connectionStateListeners.add(listener);
    listener(this.connectionState);
    return () => {
      this.connectionStateListeners.delete(listener);
    };
  }

  public onEvent(callback: EventCallback): () => void {
    this.eventListeners.add(callback);
    return () => {
      this.eventListeners.delete(callback);
    };
  }

  public onThreat(callback: ThreatCallback): () => void {
    this.threatListeners.add(callback);
    return () => {
      this.threatListeners.delete(callback);
    };
  }

  public onThreatUpdate(callback: ThreatUpdateCallback): () => void {
    this.threatUpdateListeners.add(callback);
    return () => {
      this.threatUpdateListeners.delete(callback);
    };
  }

  public onAppEvent(applicationId: string, callback: AppEventCallback): () => void {
    if (!this.appListeners.has(applicationId)) {
      this.appListeners.set(applicationId, new Set());
    }
    const listeners = this.appListeners.get(applicationId)!;
    listeners.add(callback);

    if (this.client && this.client.connected) {
      this.subscribeAppTopic(applicationId);
    }

    return () => {
      listeners.delete(callback);
      if (listeners.size === 0) {
        this.appListeners.delete(applicationId);
        const sub = this.activeSubscriptions.get(`/topic/security/applications/${applicationId}`);
        if (sub) {
          sub.unsubscribe();
          this.activeSubscriptions.delete(`/topic/security/applications/${applicationId}`);
        }
      }
    };
  }

  private setConnectionState(state: ConnectionState): void {
    this.connectionState = state;
    this.connectionStateListeners.forEach((listener) => listener(state));
  }

  private subscribeTopics(): void {
    if (!this.client || !this.client.connected) return;

    // 1. Subscribe to events
    if (!this.activeSubscriptions.has('/topic/security/events')) {
      const eventSub = this.client.subscribe('/topic/security/events', (message: IMessage) => {
        try {
          const raw: any = JSON.parse(message.body);
          const eventItem: SecurityEvent = raw.eventPayload || (raw.type === 'API_EVENT' ? raw.payload : raw);
          if (eventItem && (eventItem.eventId || eventItem.endpoint)) {
            if (!eventItem.id && eventItem.eventId) {
              eventItem.id = eventItem.eventId;
            }
            this.eventListeners.forEach((cb) => cb(eventItem));
          }
        } catch (err) {
          console.error('Failed to parse WebSocket event payload', err);
        }
      });
      this.activeSubscriptions.set('/topic/security/events', eventSub);
    }

    // 2. Subscribe to threats
    if (!this.activeSubscriptions.has('/topic/security/threats')) {
      const threatSub = this.client.subscribe('/topic/security/threats', (message: IMessage) => {
        try {
          const raw: any = JSON.parse(message.body);
          const threatItem: any = raw.threatPayload || ((raw.type === 'THREAT_DETECTED' || raw.type === 'THREAT_UPDATED') ? raw.payload : raw);
          if (threatItem && (threatItem.id || threatItem.threatId || threatItem.threatType)) {
            if (!threatItem.id && threatItem.threatId) {
              threatItem.id = threatItem.threatId;
            }
            if (raw.type === 'THREAT_UPDATED') {
              this.threatUpdateListeners.forEach((cb) => cb(threatItem));
            } else {
              this.threatListeners.forEach((cb) => cb(threatItem));
            }
          }
        } catch (err) {
          console.error('Failed to parse WebSocket threat payload', err);
        }
      });
      this.activeSubscriptions.set('/topic/security/threats', threatSub);
    }

    // 3. Resubscribe any registered application topics
    this.appListeners.forEach((_, appId) => {
      this.subscribeAppTopic(appId);
    });
  }

  private subscribeAppTopic(applicationId: string): void {
    const topic = `/topic/security/applications/${applicationId}`;
    if (this.client && this.client.connected && !this.activeSubscriptions.has(topic)) {
      const sub = this.client.subscribe(topic, (message: IMessage) => {
        try {
          const raw: any = JSON.parse(message.body);
          const eventItem: SecurityEvent = raw.eventPayload || (raw.type === 'API_EVENT' ? raw.payload : raw);
          if (eventItem && (eventItem.eventId || eventItem.endpoint)) {
            if (!eventItem.id && eventItem.eventId) {
              eventItem.id = eventItem.eventId;
            }
            const listeners = this.appListeners.get(applicationId);
            if (listeners) {
              listeners.forEach((cb) => cb(eventItem));
            }
          }
        } catch (err) {
          console.error(`Failed to parse WebSocket app topic payload for ${applicationId}`, err);
        }
      });
      this.activeSubscriptions.set(topic, sub);
    }
  }

  private clearSubscriptions(): void {
    this.activeSubscriptions.forEach((sub) => sub.unsubscribe());
    this.activeSubscriptions.clear();
  }
}

export const securitySocketManager = new SecuritySocketManager();
