import { SecurityEvent } from './event';
import { Threat } from './threat';

export type RealtimeMessageType = 'API_EVENT' | 'THREAT_DETECTED' | 'THREAT_UPDATED';

export interface RealtimeSecurityMessage {
  type: RealtimeMessageType;
  timestamp: string;
  applicationId: string;
  eventPayload?: SecurityEvent;
  threatPayload?: Threat;
}
