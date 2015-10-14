// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: fullnames 

package org.boldlygoingnowhere.yahoobot;

public final class Randomizer
{

    public Randomizer(long seed)  // seed()
    {
        seed_randomizer(seed);
    }

    public final synchronized void seed_randomizer(long seed) // Np (int64 el)
    {
        current_value = (seed ^ 0x5deece66dL) & 0xffffffffffffL;
    }

    public final synchronized int get_next_large_number(int i)   //  int32 Jp (int i)
    {
        long l = current_value * 0x5deece66dL + 11L & 0xffffffffffffL;
        current_value = l;
        return (int)(l >>> 48 - i);
    }

    public final int blah()    // uint32_t Lp()
    {
        return get_next_large_number(32);
    }

    public final int get_next_number(int i)   // uint32_t Ip(int i)
    {
        return (blah() & 0x7fffffff) % i;
    }


    private long current_value;
}
