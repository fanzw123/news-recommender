#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# Downloads the Reuters dataset and prepares it for clustering
#


#MAHOUT_BIN="/Users/teo/DP/mahout-distribution-0.5/bin"
#cd $MAHOUT_BIN
MAHOUT="/Users/teo/DP/mahout-distribution-0.5/bin/mahout"

if [ ! -e $MAHOUT ]; then
  echo "Can't find mahout driver in $MAHOUT, cwd `pwd`, exiting.."
  exit 1
fi
 
 INPUT_DIR=$1
 INPUT_PATH=$INPUT_DIR/mahout
 TOPICS=$2
 NUM_WORDS=$3

rm -rf $INPUT_PATH


$MAHOUT seqdirectory -i $INPUT_DIR -o $INPUT_PATH/seqdir -c UTF-8 \
&& \

  $MAHOUT seq2sparse \
    -i $INPUT_PATH/seqdir \
    -o $INPUT_PATH/sparse \
	--maxDFPercent 90 \
    -wt tf -n 2 --minDF 5 --maxDFPercent 90\
  && \
  $MAHOUT lda \
    -i $INPUT_PATH/sparse/tf-vectors \
    -o $INPUT_PATH/lda -k $TOPICS -v $NUM_WORDS -ow -x 50 \
  && \
  $MAHOUT ldatopics \
    -i $INPUT_PATH/lda/state-50 \
    -d $INPUT_PATH/sparse/dictionary.file-0 \
    -dt sequencefile -o $INPUT_PATH/out

