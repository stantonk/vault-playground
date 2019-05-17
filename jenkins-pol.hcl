# Login with AppRole
path "auth/approle/login" {
  capabilities = [ "create", "read" ]
}

# Read test data
path "database/creds/my-role" {
  capabilities = [ "read" ]
}
