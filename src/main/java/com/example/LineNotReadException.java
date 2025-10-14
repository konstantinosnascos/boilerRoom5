package com.example;
import java.io.IOException;

public class LineNotReadException extends RuntimeException
{
    public LineNotReadException (String e)
    {
        super(e);
    }
}
