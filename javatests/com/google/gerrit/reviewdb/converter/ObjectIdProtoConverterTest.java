// Copyright (C) 2019 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.reviewdb.converter;

import static com.google.common.truth.Truth.assertThat;
import static com.google.gerrit.proto.testing.SerializedClassSubject.assertThatSerializedClass;

import com.google.common.collect.ImmutableMap;
import com.google.gerrit.proto.Entities;
import com.google.gerrit.proto.testing.SerializedClassSubject;
import com.google.protobuf.Parser;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;

public class ObjectIdProtoConverterTest {
  private final ObjectIdProtoConverter objectIdProtoConverter = ObjectIdProtoConverter.INSTANCE;

  @Test
  public void allValuesConvertedToProto() {
    ObjectId objectId = ObjectId.fromString("deadbeefdeadbeefdeadbeefdeadbeefdeadbeef");

    Entities.ObjectId proto = objectIdProtoConverter.toProto(objectId);

    Entities.ObjectId expectedProto =
        Entities.ObjectId.newBuilder().setName("deadbeefdeadbeefdeadbeefdeadbeefdeadbeef").build();
    assertThat(proto).isEqualTo(expectedProto);
  }

  @Test
  public void allValuesConvertedToProtoAndBackAgain() {
    ObjectId objectId = ObjectId.fromString("deadbeefdeadbeefdeadbeefdeadbeefdeadbeef");

    ObjectId convertedObjectId =
        objectIdProtoConverter.fromProto(objectIdProtoConverter.toProto(objectId));

    assertThat(convertedObjectId).isEqualTo(objectId);
  }

  @Test
  public void protoCanBeParsedFromBytes() throws Exception {
    Entities.ObjectId proto =
        Entities.ObjectId.newBuilder().setName("deadbeefdeadbeefdeadbeefdeadbeefdeadbeef").build();
    byte[] bytes = proto.toByteArray();

    Parser<Entities.ObjectId> parser = objectIdProtoConverter.getParser();
    Entities.ObjectId parsedProto = parser.parseFrom(bytes);

    assertThat(parsedProto).isEqualTo(proto);
  }

  /** See {@link SerializedClassSubject} for background and what to do if this test fails. */
  @Test
  public void fieldsExistAsExpected() {
    assertThatSerializedClass(ObjectId.class)
        .hasFields(
            ImmutableMap.of(
                "w1", int.class,
                "w2", int.class,
                "w3", int.class,
                "w4", int.class,
                "w5", int.class));
  }
}
