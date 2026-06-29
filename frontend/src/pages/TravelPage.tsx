import { moduleDefinitions } from '../lib/modules';
import { ConversationPage } from './ConversationPage';

export function TravelPage() {
  return <ConversationPage module={moduleDefinitions.travel} />;
}
