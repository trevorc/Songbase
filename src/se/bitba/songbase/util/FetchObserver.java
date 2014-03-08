/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.util;

public interface FetchObserver<T>
{
    public void onSuccess(T result);
    public void onFailure();
}
