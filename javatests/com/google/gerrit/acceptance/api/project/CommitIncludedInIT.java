// Copyright (C) 2017 The Android Open Source Project
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

package com.google.gerrit.acceptance.api.project;

import static com.google.common.truth.Truth.assertThat;
import static com.google.gerrit.acceptance.testsuite.project.TestProjectUpdate.allow;
import static org.eclipse.jgit.lib.Constants.R_TAGS;

import com.google.gerrit.acceptance.AbstractDaemonTest;
import com.google.gerrit.acceptance.NoHttpd;
import com.google.gerrit.acceptance.PushOneCommit.Result;
import com.google.gerrit.acceptance.testsuite.project.ProjectOperations;
import com.google.gerrit.common.data.Permission;
import com.google.gerrit.extensions.api.changes.IncludedInInfo;
import com.google.gerrit.extensions.api.changes.ReviewInput;
import com.google.gerrit.extensions.api.projects.TagInput;
import com.google.gerrit.reviewdb.client.BranchNameKey;
import com.google.inject.Inject;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;

@NoHttpd
public class CommitIncludedInIT extends AbstractDaemonTest {
  @Inject private ProjectOperations projectOperations;

  @Test
  public void includedInOpenChange() throws Exception {
    Result result = createChange();
    assertThat(getIncludedIn(result.getCommit().getId()).branches).isEmpty();
    assertThat(getIncludedIn(result.getCommit().getId()).tags).isEmpty();
  }

  @Test
  public void includedInMergedChange() throws Exception {
    Result result = createChange();
    gApi.changes()
        .id(result.getChangeId())
        .revision(result.getCommit().name())
        .review(ReviewInput.approve());
    gApi.changes().id(result.getChangeId()).revision(result.getCommit().name()).submit();

    assertThat(getIncludedIn(result.getCommit().getId()).branches).containsExactly("master");
    assertThat(getIncludedIn(result.getCommit().getId()).tags).isEmpty();

    projectOperations
        .project(project)
        .forUpdate()
        .add(allow(Permission.CREATE_TAG).ref(R_TAGS + "*").group(adminGroupUuid()))
        .update();
    gApi.projects().name(result.getChange().project().get()).tag("test-tag").create(new TagInput());

    assertThat(getIncludedIn(result.getCommit().getId()).tags).containsExactly("test-tag");

    createBranch(BranchNameKey.create(project, "test-branch"));

    assertThat(getIncludedIn(result.getCommit().getId()).branches)
        .containsExactly("master", "test-branch");
  }

  private IncludedInInfo getIncludedIn(ObjectId id) throws Exception {
    return gApi.projects().name(project.get()).commit(id.name()).includedIn();
  }
}
