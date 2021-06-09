/**
 * Copyright (C) 2016 Newland Group Holding Limited
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zakl.msgdistribute;


import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MessageCallBack<T> {

    private final Lock lock = new ReentrantLock();
    private final Condition finish = lock.newCondition();

    private T response;

    public T start() {
        try {
            lock.lock();
            await();
            return response;
        } finally {
            lock.unlock();
        }
    }

    public void over(T result) {
        try {
            lock.lock();
            finish.signal();
            response = result;
        } finally {
            lock.unlock();
        }
    }

    @SneakyThrows
    private void await() {
        boolean timeout = false;
        try {
            timeout = finish.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!timeout) {
            throw new TimeoutException("passiveCall time out...");
        }
    }


}
