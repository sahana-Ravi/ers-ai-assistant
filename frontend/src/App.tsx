import { useRef, useState } from 'react'
import { Bot, CheckCheck, SendHorizontal } from 'lucide-react'
import { sendChatMessage } from './api/chatApi'
import './App.css'

type Message = { id: string; role: 'assistant' | 'user'; content: string; status?: 'loading' | 'error' }

const initialMessage: Message = {
  id: 'welcome', role: 'assistant',
  content: 'Ask me about ERS data or the documents available to the assistant. I can search approved documentation and use the read-only ERS database tools when needed.',
}

function App() {
  const [messages, setMessages] = useState<Message[]>([initialMessage])
  const [input, setInput] = useState('')
  const [isSending, setIsSending] = useState(false)
  const inputRef = useRef<HTMLTextAreaElement>(null)

  const submitMessage = async (message: string) => {
    const trimmedMessage = message.trim()
    if (!trimmedMessage || isSending) return
    const assistantMessageId = crypto.randomUUID()
    setMessages((current) => [...current, { id: crypto.randomUUID(), role: 'user', content: trimmedMessage }, { id: assistantMessageId, role: 'assistant', content: '', status: 'loading' }])
    setInput('')
    setIsSending(true)
    try {
      const answer = await sendChatMessage(trimmedMessage)
      setMessages((current) => current.map((messageItem) => messageItem.id === assistantMessageId ? { ...messageItem, content: answer, status: undefined } : messageItem))
    } catch (error) {
      const detail = error instanceof Error ? error.message : 'The assistant could not respond.'
      setMessages((current) => current.map((messageItem) => messageItem.id === assistantMessageId ? { ...messageItem, content: detail, status: 'error' } : messageItem))
    } finally {
      setIsSending(false)
      inputRef.current?.focus()
    }
  }

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => { event.preventDefault(); void submitMessage(input) }
  const handleKeyDown = (event: React.KeyboardEvent<HTMLTextAreaElement>) => { if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); void submitMessage(input) } }

  return <main className="application-shell">
    <section className="chat-pane" aria-label="ERS AI Assistant">
      <header className="chat-header"><h1>ERS AI Assistant</h1></header>
      <div className="chat-scroll-area"><div className="chat-content">
        {messages.map((message, index) => <article className={`message message--${message.role}`} key={message.id}><div className="message__avatar">{message.role === 'assistant' ? <Bot size={19} /> : 'D'}</div><div className="message__body"><div className="message__meta"><strong>{message.role === 'assistant' ? 'ERS AI Assistant' : 'You'}</strong>{message.role === 'assistant' && index !== 0 && !message.status && <span className="message__source"><CheckCheck size={14} /> ERS response</span>}</div>{message.status === 'loading' ? <div className="typing-indicator" aria-label="Assistant is thinking"><i /><i /><i /></div> : <p className={`message__text ${message.status === 'error' ? 'message__text--error' : ''}`}>{message.content}</p>}</div></article>)}
      </div></div>
      <footer className="composer-area"><form className="composer" onSubmit={handleSubmit}><textarea ref={inputRef} value={input} onChange={(event) => setInput(event.target.value)} onKeyDown={handleKeyDown} placeholder="Ask about ERS data or documentation..." rows={1} aria-label="Message ERS AI Assistant" disabled={isSending} /><button className="send-button" type="submit" disabled={!input.trim() || isSending} aria-label="Send message"><SendHorizontal size={19} /></button></form></footer>
    </section>
  </main>
}

export default App
