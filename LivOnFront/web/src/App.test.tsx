import { render, screen } from '@testing-library/react';
import App from './App';

test('renders LivOn title', () => {
  render(<App />);
  const titleElement = screen.getByText(/LivOn/i);
  expect(titleElement).toBeInTheDocument();
});