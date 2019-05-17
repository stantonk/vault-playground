# Login with AppRole
path "auth/approle/login" {
  capabilities = [ "create", "read" ]
}

# Read test data
# Set the path to "secret/data/mysql/*" if you are running kv-h2
path "secret/mysql/*" {
  capabilities = [ "read" ]
}
