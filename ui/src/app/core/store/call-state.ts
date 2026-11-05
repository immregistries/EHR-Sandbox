/** Shared loading/error shape used by every feature slice. */
export type CallState = 'init' | 'loading' | 'loaded' | { error: string };

export const getError = (callState: CallState): string | null =>
  typeof callState === 'object' && 'error' in callState ? callState.error : null;

export const isLoading = (callState: CallState): boolean => callState === 'loading';
