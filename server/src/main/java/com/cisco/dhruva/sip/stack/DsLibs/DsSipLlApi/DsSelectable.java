// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsUnitOfWork;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * The selectable interface that can registered to the {@link DsSelector} through {@link
 * DsSelector#register(DsSelectable) register}. The selectable concrete implementation would be
 * registered to the {@link DsSelector} for the operation(s), as specified by {@link #getOperation()
 * getOperation()}, that may be performed on the {@link #getChannel() channel}. Whenever there is an
 * operation ready to be performed on the {@link #getChannel() channel}, the {@link DsSelector} will
 * notify this selectable instance by invoking {@link DsUnitOfWork#process() process()}.
 */
public interface DsSelectable extends DsUnitOfWork {
  /**
   * Returns the ID of this selectable channel.
   *
   * @return the ID of this selectable channel.
   */
  public int getID();

  /**
   * The underlying selectable channel for which this instance is interested in performing various
   * operations.
   *
   * @return the underlying selectable channel where ready operations can be performed.
   */
  public SelectableChannel getChannel();

  /**
   * The various operations that this selectable instance is interested in performing on the
   * underlying selectable channel, when these operations are ready to perform.
   *
   * @return the bit mask of interested operations.
   */
  public int getOperation();

  /**
   * Sets the selection key. This is necessary since the registration of a selectable is done
   * aynchronously.
   *
   * @param sk the selection key to associate with this selectable
   */
  public void setSelectionKey(SelectionKey sk);

  /**
   * Get the selection key associated with this selectable.
   *
   * @return the selection key associated with this selectable
   */
  public SelectionKey getSelectionKey();
}
