#!/bin/bash
echo ''
echo ''
echo '   ___             ___           __  __  '
echo '  | _ \    ___    / __|   _  _  |  \/  | '
echo '  |   /   / -_)   \__ \  | +| | | |\/| | '
echo '  |_|_\   \___|   |___/   \_,_| |_|__|_| '
echo '_|"""""|_|"""""|_|"""""|_|"""""|_|"""""| '
echo $'"`-0-0-\'"`-0-0-\'"`-0-0-\'"`-0-0-\'"`-0-0-\' '
echo -e '\n\n'

# Loading configurations for experiments
echo '>> Loading config file config.cfg'
source config2.cfg
cat config2.cfg

unset datasets
declare -A datasets
datasets[$citeseer_db]=$citeseer_defaults
datasets[$fb_computer_db]=$fb_computer_defaults
datasets[$fb_travel_db]=$fb_travel_defaults
datasets[$amazon_db]=$amazon_defaults

unset test_freqs
declare -A test_freqs
test_freqs[$citeseer_db]=$citeseer_freqs
test_freqs[$fb_computer_db]=$fb_computer_freqs
test_freqs[$fb_travel_db]=$fb_travel_freqs
test_freqs[$amazon_db]=$amazon_freqs

unset flags
declare -A flags
flags[$citeseer_db]=$citeseer_flags
flags[$fb_computer_db]=$fb_computer_flags
flags[$fb_travel_db]=$fb_travel_flags
flags[$amazon_db]=$amazon_flags

echo -e '\n\n>> Creating directories ...'
mkdir -p $output_data

append_with_newline() { printf -v "$1" '%s\n%s' "${!1}" "$2"; }

for dataset in ${!datasets[@]}
do
	dataset_path="$input_data"
	default=${datasets[${dataset}]}
	flag=${flags[${dataset}]}
	# Parse default values
	defaults=(`echo $default|tr "," "\n"`)
	experiments=(`echo $flag|tr "," "\n"`)


	echo ">> Processing dataset ${dataset} with default values (${defaults[@]})"
	echo ">> Experiment flags ${experiments[@]}"

	for num_users in ${funcs[*]}
	do
		# For num_users, fixed relevance, varying frequency
		if [[ ${experiments[0]} -eq "1" ]]; then
			echo '-----------------------------'
			echo '      Varying frequency 	   '
			echo '-----------------------------'

			OUTPUT="$output_data/u${num_users}/frequency"
			mkdir -p $OUTPUT

			freqs=(`echo ${test_freqs[${dataset}]}|tr "," "\n"`)

			for freq in ${freqs[*]}
			do
				for task in ${tasks[*]}
				do
					echo "Running command ..."
					echo "$JVM $ReSuM_jar filename=${dataset}.lg weightFile=${dataset}_$disp.w disp=$disp freq=$freq rel=${defaults[1]} multipleRuns=$multirun task=$task functions=${num_users} structureSize=${num_users} ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=$OUTPUT"
					echo "---- `date`"
					$JVM $ReSuM_jar filename=${dataset}.lg weightFile=${dataset}_$disp.w disp=$disp freq=$freq rel=${defaults[1]} multipleRuns=$multirun task=$task functions=${num_users} structureSize=${num_users} ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=$OUTPUT
				done
			done
		fi
		# For num_users, fixed frequency, varying relevance
		if [[ ${experiments[1]} -eq "1" ]]; then
			echo '-----------------------------'
			echo '      Varying relevance 	   '
			echo '-----------------------------'

			OUTPUT="$output_data/u${num_users}/relevance"
			mkdir -p $OUTPUT

			for rel in ${rels[*]}
			do
				for task in ${tasks[*]}
				do
					echo "Running command ..."
					echo "$JVM $ReSuM_jar filename=${dataset}.lg weightFile=${dataset}_$disp.w disp=$disp freq=${defaults[0]} rel=$rel multipleRuns=$multirun task=$task functions=${num_users} structureSize=${num_users} ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=$OUTPUT"
					echo "---- `date`"
					$JVM $ReSuM_jar filename=${dataset}.lg weightFile=${dataset}_$disp.w disp=$disp freq=${defaults[0]} rel=$rel multipleRuns=$multirun task=$task functions=${num_users} structureSize=${num_users} ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=$OUTPUT
				done
			done
		fi
		# For num_users, fixed frequency and relevance, varying score
		if [[ ${experiments[2]} -eq "1" ]]; then
			echo '-----------------------------'
			echo '        Varying scores 	   '
			echo '-----------------------------'

			OUTPUT="$output_data/u${num_users}/score"
			mkdir -p $OUTPUT

			for task in ${tasks[*]}
			do
				echo "Running command ..."
				echo "$JVM $ReSuM_jar filename=${dataset}.lg weightFile=${dataset}_$disp.w disp=$disp freq=${defaults[0]} rel=${defaults[1]} multipleRuns=$multirun task=$task functions=${num_users} structureSize=${num_users} ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=$OUTPUT"
				echo "---- `date`"
				$JVM $ReSuM_jar filename=${dataset}.lg weightFile=${dataset}_$disp.w disp=$disp freq=${defaults[0]} rel=${defaults[1]} multipleRuns=$multirun task=$task functions=${num_users} structureSize=${num_users} ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=$OUTPUT
			done
		fi
		# Clustering - Preprocessing step
		if [[ ${experiments[3]} -eq "1" ]]; then
			echo '-----------------------------------------'
			echo '    	        Clustering                 '
			echo '-----------------------------------------'

			num_clusters=$(( num_users/cluster_ratio ))
			OUTPUT="$output_data/u${num_users}/clustering"
			mkdir -p $OUTPUT

			if [[ $clustType == "path" ]] ; then
				PATHFOLDER="${input_data}/${dataset}/path"
				mkdir -p $PATHFOLDER
				for task in ${tasks[*]}
				do
					OUTPUTFOLDER="$PATHFOLDER/T$task"
					mkdir -p $OUTPUTFOLDER

					echo "Running command ..."
					echo "$JVM $clustering_jar filename=${dataset}.lg weightFile=${dataset}_$disp.w disp=$disp random=$random clusteringType=$clustType functions=${num_users} structureSize=${num_clusters} ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=${input_data}/${dataset} | tee ${OUTPUT}/clustering_${dataset}_C${num_clusters}_T${task}.log"
					echo "---- `date`"
					$JVM $clustering_jar filename=${dataset}.lg weightFile=${dataset}_$disp.w disp=$disp random=$random clusteringType=$clustType functions=${num_users} structureSize=${num_clusters} ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=${input_data}/${dataset} | tee ${OUTPUT}/clustering_${dataset}_C${num_clusters}_T$task.log

					mv ${input_data}/${dataset}/${dataset}_${disp}_${num_clusters}.cw $OUTPUTFOLDER/
					mv ${input_data}/${dataset}/${dataset}_${disp}_${num_clusters}.cl $OUTPUTFOLDER/
				done
			elif [[ $clustType == "aposteriori" ]] ; then
				PATTERNFILE="$output_data/u${num_users}/scores"
				OUTPUTAP="$output_data/u${num_users}/clustering/aposteriori"
				mkdir -p $OUTPUTAP
				for task in ${tasks[*]}
				do
					WEIGHTFILEFOLDER="${input_data}/${dataset}/aposteriori"
                    			mkdir -p $WEIGHTFILEFOLDER
                    			TASKSUBFOLDER="$WEIGHTFILEFOLDER/T${task}"
					mkdir -p $TASKSUBFOLDER

					echo "Running command ..."
					echo "$JVM $clustering_jar filename=${dataset}.lg weightFile=${dataset}_$disp.w disp=$disp patternFile=$PATTERNFILE/Patterns_${dataset}.lg_F${defaults[0]}R${defaults[1]}T${task}C${num_users}RUN0.p functions=${num_users} structureSize=${num_clusters} ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=${input_data}/${dataset} | tee ${OUTPUTAP}/clustering_${dataset}_C${num_clusters}_POST_T${task}.log"
					echo "---- `date`"
					$JVM $clustering_jar filename=${dataset}.lg weightFile=${dataset}_$disp.w disp=$disp patternFile=$PATTERNFILE/Patterns_${dataset}.lg_F${defaults[0]}R${defaults[1]}T${task}C${num_users}RUN0.p random=$random clusteringType=$clustType functions=${num_users} structureSize=${num_clusters} ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=${input_data}/${dataset} | tee ${OUTPUT}/clustering_${dataset}_C${num_clusters}_POST_T$task.log

					mv ${input_data}/${dataset}/${dataset}_${disp}_${num_clusters}.cw $TASKSUBFOLDER/
					mv ${input_data}/${dataset}/${dataset}_${disp}_${num_clusters}.cl $TASKSUBFOLDER/
				done
			else
				echo "Running command ..."
				echo "$JVM $clustering_jar filename=${dataset}.lg weightFile=${dataset}_$disp.w buckets=${defaults[3]} disp=$disp random=$random clusteringType=$clustType functions=${num_users} structureSize=$num_clusters smart=$smart ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=${input_data}/${dataset} | tee ${OUTPUT}/clustering_${dataset}_B${defaults[3]}_C${num_clusters}.log"
				echo "---- `date`"
				$JVM $clustering_jar filename=${dataset}.lg weightFile=${dataset}_$disp.w buckets=${defaults[3]} disp=$disp random=$random clusteringType=$clustType functions=${num_users} structureSize=$num_clusters smart=$smart ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=${input_data}/${dataset} | tee ${OUTPUT}/clustering_${dataset}_B${defaults[3]}_C${num_clusters}.log
			fi
		fi
		# Approximate
		if [[ ${experiments[4]} -eq "1" ]]; then
			echo '-----------------------------------------'
			echo ' 		        Approximate                '
			echo '-----------------------------------------'

			num_clusters=$(( num_users/cluster_ratio ))
			OUTPUTAX="$output_data/u${num_users}/approx"
			mkdir -p $OUTPUTAX

			for task in ${tasks[*]}
			do
				if [[ $clustType == "path" ]] ; then
					echo "Running command ..."
					echo "$JVM $ReSuM_jar filename=${dataset}.lg weightFile=path/T${task}/${dataset}_${disp}_${num_clusters}.cw disp=${disp} freq=${defaults[0]} rel=${defaults[1]} task=$task multipleRuns=$multirun functions=${num_users} structureSize=${num_clusters} clusteringType=$clustType ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=$OUTPUTAX"
					echo "---- `date`"
					$JVM $ReSuM_jar filename=${dataset}.lg weightFile=path/T${task}/${dataset}_${disp}_${num_clusters}.cw disp=${disp} freq=${defaults[0]} rel=${defaults[1]} task=$task multipleRuns=$multirun functions=${num_users} structureSize=${num_clusters} clusteringType=$clustType ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=$OUTPUTAX
				elif [[ $clustType == "aposteriori" ]] ; then
					OUTPUTAXAP="$output_data/u${num_users}/approx/aposteriori"
					mkdir -p $OUTPUTAXAP

					echo "Running command ..."
					echo "$JVM $ReSuM_jar filename=${dataset}.lg weightFile=aposteriori/T${task}/${dataset}_${disp}_${num_clusters}.cw disp=${disp} freq=${defaults[0]} rel=${defaults[1]} task=$task multipleRuns=$multirun functions=${num_users} structureSize=${num_clusters} clusteringType=$clustType ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=$OUTPUTAXAP"
					echo "---- `date`"
					$JVM $ReSuM_jar filename=${dataset}.lg weightFile=aposteriori/T${task}/${dataset}_${disp}_${num_clusters}.cw disp=${disp} freq=${defaults[0]} rel=${defaults[1]} task=$task multipleRuns=$multirun functions=${num_users} structureSize=${num_clusters} clusteringType=$clustType ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=$OUTPUTAXAP
				else
					echo "Running command ..."
					echo "$JVM $ReSuM_jar filename=${dataset}.lg weightFile=${dataset}_${disp}_${num_clusters}.cw disp=${disp} freq=${defaults[0]} rel=${defaults[1]} task=$task multipleRuns=$multirun functions=${num_users} structureSize=${num_clusters} clusteringType=$clustType buckets=${defaults[3]} ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=$OUTPUTAX"
					echo "---- `date`"
					$JVM $ReSuM_jar filename=${dataset}.lg weightFile=${dataset}_${disp}_${num_clusters}.cw disp=${disp} freq=${defaults[0]} rel=${defaults[1]} task=$task multipleRuns=$multirun functions=${num_users} structureSize=${num_clusters} clusteringType=$clustType buckets=${defaults[3]} ignoreLabels=${defaults[4]} datasetFolder=${input_data}/${dataset} outputFolder=$OUTPUTAX
				fi
			done
		fi
	done
done
echo 'Terminated.'
