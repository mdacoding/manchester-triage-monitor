import React from 'react';

/**
 * React Error Boundary component.
 * Catches JavaScript errors anywhere in their child component tree,
 * logs those errors, and displays a fallback UI instead of crashing the whole app.
 */
export class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    // Update state so the next render will show the fallback UI.
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error("ErrorBoundary caught an error:", error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="p-4 bg-red-50 border border-red-200 rounded-xl m-4 text-red-800">
          <h2 className="font-semibold text-lg mb-2">Da ist etwas schiefgelaufen.</h2>
          <p className="text-sm">Bitte lade die Seite neu. Wenn das Problem weiterhin besteht, kontaktiere den Support.</p>
        </div>
      );
    }

    return this.props.children;
  }
}
