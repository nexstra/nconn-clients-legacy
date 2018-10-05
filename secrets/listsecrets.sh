
region=${1:-$(aws configure get region)}
[ -z "$region" ] && { echo "Region required" ; exit 1 ; }
printf "Listing secrets for region: %s\n" "$region"
mkdir -p "$region"
 aws secretsmanager list-secrets | tee secrets.json | jq -r '.[][]|.Name' | while read name ; do
 path="${region}/${name}"
 echo "$path"
 mkdir -p "$(dirname $path)" 
 while ! aws secretsmanager describe-secret --secret-id "$name" > "${path}.json" ; do 
   sleep 1 
 done
done
