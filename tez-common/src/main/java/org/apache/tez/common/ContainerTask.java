/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.tez.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class ContainerTask implements Writable {

  TezTaskContext tezTaskContext;
  boolean shouldDie;

  public ContainerTask() {
  }

  public ContainerTask(TezTaskContext tezTaskContext, boolean shouldDie) {
    this.tezTaskContext = tezTaskContext;
    this.shouldDie = shouldDie;
  }

  public TezTaskContext getTezEngineTaskContext() {
    return tezTaskContext;
  }

  public boolean shouldDie() {
    return shouldDie;
  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeBoolean(shouldDie);
    if (tezTaskContext != null) {
      out.writeBoolean(true);
      Text.writeString(out, tezTaskContext.getClass().getName());
      tezTaskContext.write(out);
    } else {
      out.writeBoolean(false);
    }
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    shouldDie = in.readBoolean();
    boolean taskComing = in.readBoolean();
    if (taskComing) {
      String contextClass = Text.readString(in);
      tezTaskContext = createEmptyContext(contextClass);
      tezTaskContext.readFields(in);
    }
  }

  private TezTaskContext createEmptyContext(String contextClassName)
      throws IOException {
    try {
      Class<?> clazz = Class.forName(contextClassName);
      Constructor<?> c = clazz.getConstructor(null);
      c.setAccessible(true);
      return (TezTaskContext) c.newInstance(null);
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    } catch (SecurityException e) {
      throw new IOException(e);
    } catch (NoSuchMethodException e) {
      throw new IOException(e);
    } catch (IllegalArgumentException e) {
      throw new IOException(e);
    } catch (InstantiationException e) {
      throw new IOException(e);
    } catch (IllegalAccessException e) {
      throw new IOException(e);
    } catch (InvocationTargetException e) {
      throw new IOException(e);
    }
  }

  @Override
  public String toString() {
    return "shouldDie: " + shouldDie + ", tezEngineTaskContext: "
        + tezTaskContext;
  }
}
