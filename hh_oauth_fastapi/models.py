from sqlalchemy import Column, BigInteger, Text
from .database import Base

class Profile(Base):
    __tablename__ = 'profiles'

    chat_id = Column(BigInteger, primary_key=True)
    access_token = Column(Text, nullable=False)
    refresh_token = Column(Text, nullable=False)
