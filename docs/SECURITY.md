# Security

## Threat Model

- **Untrusted client input**: endpoints validate JSON payload structure and required fields.
- **Local-only demo**: services bind to localhost by default and store data in memory.
- **No secrets required**: all demo credentials are fixed and local.

## Safe Defaults

- Observability and structured logging are disabled unless explicitly enabled via environment variables.
- No external network calls are made during tests or demo flows.

## Intentional Limits

- Demo tokens are deterministic and not intended for production use.
- Data is stored only in memory for deterministic behavior.
