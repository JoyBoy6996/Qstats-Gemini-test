install_apk() {
  path="$1"
  session=$(pm install-create -r -d | sed 's/.*\[\(.*\)\].*/\1/')
  [ -z "$session" ] && return 1
  size=$(wc -c < "$path")
  cat "$path" | pm install-write -S "$size" "$session" base.apk - || {
    pm install-abandon "$session" 2>/dev/null
    return 1
  }
  pm install-commit "$session"
}
install_apk "${APK}" 2>&1
