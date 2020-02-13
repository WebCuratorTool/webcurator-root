#!/bin/sh


# This script takes our existing repos and merges them in the new
# repo webcurator-root. In the final run of this script, when we're
# really ready to merge, all branches should be master branches. For
# now we select our branches to contain functioning gradle builds.



# directory where we will do the merging
WORKSPACE=/usr/local/tmp/wct3




# We assume there's a new empty repo webcurator-root
cd $WORKSPACE
git clone https://github.com/WebCuratorTool/webcurator-root.git
cd webcurator-root

repos="webcurator-legacy-lib-dependencies webcurator-db webcurator-core webcurator-webapp webcurator-docs webcurator-harvest-agent-h3 webcurator-harvest-agent-h1 webcurator-submit-to-rosetta webcurator-store"

# Create expression for grep: "webcurator-core|webcurator-webapp|...", needed when moving repos into their own subdir
dir_names_disjunction=`echo -n $repos | tr " " "|"`

for repo in $repos
do

	echo "Processing repo $repo"

	# Add remote repo webcurator-$suffix
	git remote add -f $repo https://github.com/WebCuratorTool/$repo.git 

	# Temporary code to make sure we only get gradle projects here
	branch="master"
	if [ "$repo" = "webcurator-core" ]
	then
		branch="v3/controllers-ti-bugfix"
	fi 
	if [ "$repo" = "webcurator-webapp" ]
	then
		branch="v3/controllers-ti-bugfix"
	fi 
	if [ "$repo" = "webcurator-harvest-agent-h1" ]
	then
		branch="v3/mvn-to-gradle"
	fi 

	echo "Merging $repo/$branch into webcurator-root"

	# Merge current repo into webcurator-root
	git merge $repo/$branch --allow-unrelated-histories -m "[REMERGE REPOS] Merged $repo"

	# Resolve inevitable conflicts in .gitignore: we'll end up with the last .gitignore we encounter
	# There's also a LICENSE file somewhere with an odd minor difference: we'll keep the version of webcurator-root
	git checkout $repo/$branch .gitignore
	git checkout LICENSE
	git commit -a -m "[REMERGE REPOS] Merged $repo"


	# Move this repo into its own subdir (Note: only webcurator-root has an README.md, the others have README.rst files)
	mkdir $repo
	git add $repo
	for f in `ls -A | grep -vE $dir_names_disjunction | grep -v .git | grep -v README.md | grep -v LICENSE`
	do
		git mv $f $repo/
	done

	echo "Committing move of $repo into its own subdir"

	# Commit the move
	git commit -a -m"[REMERGE REPOS] Moved $repo into its own subdir"

done


# Setup gradle build
gradle init --type basic --dsl groovy --project-name webcurator-root
echo "rootProject.name = 'webcurator-root'" > settings.gradle
include_line=`echo $repos | sed -E "s/([^ ]+)/'\1',/g" | sed "s/,$//"`
echo "include $include_line" >> settings.gradle

# Add gradle stuff to git
git add gradle*
git add settings.gradle
git add build.gradle
git commit -a -m"Added root-level gradle config"


# Things to fix manually before trying to build everything:
#
# Remove build-scan plugin from webcurator-webapp/build.gradle
# Fix path issue in updateVersion() in webcurator-webapp/build.gradle



