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
package com.facebook.presto.hive;

import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.TupleDomain;
import com.facebook.presto.spi.type.TypeManager;
import com.google.common.base.Optional;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.serde2.columnar.BytesRefArrayWritable;
import org.apache.hadoop.hive.serde2.columnar.LazyBinaryColumnarSerDe;
import org.apache.hadoop.mapred.RecordReader;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.Properties;

import static com.facebook.presto.hive.HiveUtil.getDeserializer;

public class ColumnarBinaryHiveRecordCursorProvider
        implements HiveRecordCursorProvider
{
    @Override
    public Optional<HiveRecordCursor> createHiveRecordCursor(
            String clientId,
            Configuration configuration,
            ConnectorSession session,
            Path path,
            long start,
            long length,
            Properties schema,
            List<HiveColumnHandle> columns,
            List<HivePartitionKey> partitionKeys,
            TupleDomain<HiveColumnHandle> effectivePredicate,
            DateTimeZone hiveStorageTimeZone,
            TypeManager typeManager)
    {
        if (!usesColumnarBinarySerDe(schema)) {
            return Optional.absent();
        }

        RecordReader<?, ?> recordReader = HiveUtil.createRecordReader(clientId, configuration, path, start, length, schema, columns, typeManager);

        return Optional.<HiveRecordCursor>of(new ColumnarBinaryHiveRecordCursor<>(
                bytesRecordReader(recordReader),
                length,
                schema,
                partitionKeys,
                columns,
                hiveStorageTimeZone,
                DateTimeZone.forID(session.getTimeZoneKey().getId()),
                typeManager));
    }

    private static boolean usesColumnarBinarySerDe(Properties schema)
    {
        return getDeserializer(schema) instanceof LazyBinaryColumnarSerDe;
    }

    @SuppressWarnings("unchecked")
    private static RecordReader<?, BytesRefArrayWritable> bytesRecordReader(RecordReader<?, ?> recordReader)
    {
        return (RecordReader<?, BytesRefArrayWritable>) recordReader;
    }
}
