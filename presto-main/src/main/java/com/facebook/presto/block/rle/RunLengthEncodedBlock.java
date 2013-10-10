/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.block.rle;

import com.facebook.presto.block.BlockBuilder;
import com.facebook.presto.block.RandomAccessBlock;
import com.facebook.presto.operator.SortOrder;
import com.facebook.presto.serde.RunLengthBlockEncoding;
import com.facebook.presto.tuple.Tuple;
import com.facebook.presto.tuple.TupleInfo;
import com.facebook.presto.tuple.TupleReadable;
import com.google.common.base.Objects;
import io.airlift.slice.Slice;
import io.airlift.units.DataSize;
import io.airlift.units.DataSize.Unit;

import static com.google.common.base.Preconditions.checkPositionIndexes;
import static com.google.common.base.Preconditions.checkState;

public class RunLengthEncodedBlock
        implements RandomAccessBlock
{
    private final Tuple value;
    private final int positionCount;

    public RunLengthEncodedBlock(Tuple value, int positionCount)
    {
        this.value = value;
        this.positionCount = positionCount;
    }

    public Tuple getValue()
    {
        return value;
    }

    public Tuple getSingleValue()
    {
        return value;
    }

    @Override
    public int getPositionCount()
    {
        return positionCount;
    }

    @Override
    public DataSize getDataSize()
    {
        return new DataSize(value.getTupleSlice().length(), Unit.BYTE);
    }

    @Override
    public RunLengthBlockEncoding getEncoding()
    {
        return new RunLengthBlockEncoding(value.getTupleInfo());
    }

    @Override
    public RandomAccessBlock getRegion(int positionOffset, int length)
    {
        checkPositionIndexes(positionOffset, positionOffset + length, positionCount);
        return new RunLengthEncodedBlock(value, length);
    }

    @Override
    public RandomAccessBlock toRandomAccessBlock()
    {
        return this;
    }

    @Override
    public TupleInfo getTupleInfo()
    {
        return value.getTupleInfo();
    }

    @Override
    public boolean getBoolean(int position)
    {
        checkReadablePosition(position);
        return value.getBoolean();
    }

    @Override
    public long getLong(int position)
    {
        return value.getLong();
    }

    @Override
    public double getDouble(int position)
    {
        return value.getDouble();
    }

    @Override
    public Slice getSlice(int position)
    {
        return value.getSlice();
    }

    @Override
    public Tuple getTuple(int position)
    {
        checkReadablePosition(position);
        return value;
    }

    @Override
    public boolean isNull(int position)
    {
        checkReadablePosition(position);
        return value.isNull();
    }

    @Override
    public boolean equals(int position, RandomAccessBlock right, int rightPosition)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(int position, TupleReadable value)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode(int position)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(SortOrder sortOrder, int position, RandomAccessBlock right, int rightPosition)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendTupleTo(int position, BlockBuilder blockBuilder)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("value", value)
                .add("positionCount", positionCount)
                .toString();
    }

    @Override
    public RunLengthEncodedBlockCursor cursor()
    {
        return new RunLengthEncodedBlockCursor(value, positionCount);
    }

    private void checkReadablePosition(int position)
    {
        checkState(position >= 0 && position < positionCount, "position is not valid");
    }

    @Override
    public Slice getRawSlice()
    {
        return value.getSlice();
    }
}