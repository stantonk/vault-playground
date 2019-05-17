# Login with AppRole
path "auth/approle/login" {
  capabilities = [ "create", "read" ]
}

# Read mysql dynamic creds
path "database/creds/my-role" {
  capabilities = [ "read" ]
}
