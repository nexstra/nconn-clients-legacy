N=/nexstra
rm -rf repo ; mkdir repo
for d in core-dsl content-services docstore maildb nconn-v2 ; do
  echo $d
  cp -pr $N/$d/build/repo/*  repo/
  cat ../$d/build/publications.versions.properties >> repo/versions.properties
done

find repo -name \*.jar | xargs -n1 -I{} jar -xf {} schemas/
