"""In-memory storage for HH tokens."""

from typing import Dict

# chat_id -> {"access_token": str, "refresh_token": str}
tokens: Dict[int, Dict[str, str]] = {}
