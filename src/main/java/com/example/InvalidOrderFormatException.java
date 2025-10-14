package com.example;

public class InvalidOrderFormatException extends RuntimeException
{
    public InvalidOrderFormatException(String message)
    {
        super(message);
    }
}