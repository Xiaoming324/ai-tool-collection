import { moduleDefinitions } from '../lib/modules';
import { ConversationPage } from './ConversationPage';

export function ChatPage() {
  return <ConversationPage module={moduleDefinitions.chat} />;
}
