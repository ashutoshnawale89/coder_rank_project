import { AlertTriangle } from 'lucide-react';

export default function ErrorMessage({ message, onDismiss }) {
  if (!message) return null;
  return (
    <div className="error-banner" role="alert">
      <AlertTriangle size={16} />
      <span>{message}</span>
      {onDismiss && (
        <button className="btn btn-ghost btn-sm" onClick={onDismiss}>Dismiss</button>
      )}
    </div>
  );
}
