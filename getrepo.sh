N=/nexstra

for d in core-dsl docstore maildb content-services nconn-v2 ; do
  echo $d
  cp -pr $N/$d/build/repo/*  repo/
done
