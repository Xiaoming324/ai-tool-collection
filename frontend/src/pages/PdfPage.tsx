import { moduleDefinitions } from '../lib/modules';
import { ConversationPage } from './ConversationPage';

export function PdfPage() {
  return <ConversationPage module={moduleDefinitions.pdf} />;
}
