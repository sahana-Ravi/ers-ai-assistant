const CHAT_ENDPOINT = '/api/chat'

export async function sendChatMessage(message: string): Promise<string> {
  const response = await fetch(`${CHAT_ENDPOINT}?${new URLSearchParams({ message })}`, { method: 'POST', headers: { Accept: 'text/plain' } })
  if (!response.ok) throw new Error(await errorMessage(response))
  return response.text()
}

async function errorMessage(response: Response): Promise<string> {
  if (response.status === 401) return 'Your session is not authenticated. Sign in through Keycloak and try again.'
  if (response.status === 403) return 'You do not have permission to use this ERS resource.'
  if (response.status >= 500) return 'The ERS assistant is temporarily unavailable. Please try again.'
  return (await response.text()) || 'The request could not be completed.'
}