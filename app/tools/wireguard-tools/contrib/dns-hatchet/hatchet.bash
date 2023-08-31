set_dns() {
	[[ ${#DNS[@]} -gt 0 ]] || return 0

	if [[ $(resolvconf --version 2>/dev/null) == openresolv\ * ]]; then
		printf 'nameserver %s\n' "${DNS[@]}" | cmd resolvconf -a "$INTERFACE" -m 0 -x
	else
		echo "[#] mount \`${DNS[*]}' /etc/resolv.conf" >&2
		[[ -e /etc/resolv.conf ]] || touch /etc/resolv.conf
		{ cat <<-_EOF
			# This file was generated by wg-quick(8) for use with
			# the WireGuard interface $INTERFACE. It cannot be
			# removed or altered directly. You may remove this file
			# by running \`wg-quick down $INTERFACE', or if that
			# poses problems, run \`umount /etc/resolv.conf'.

		_EOF
		printf 'nameserver %s\n' "${DNS[@]}"
		} | unshare -m --propagation shared bash -c "$(cat <<-_EOF
			set -e
			context="\$(stat -c %C /etc/resolv.conf 2>/dev/null)" || unset context
			mount --make-private /dev/shm
			mount -t tmpfs none /dev/shm
			cat > /dev/shm/resolv.conf
			[[ -z \$context || \$context == "?" ]] || chcon "\$context" /dev/shm/resolv.conf 2>/dev/null || true
			mount -o remount,ro /dev/shm
			mount -o bind,ro /dev/shm/resolv.conf /etc/resolv.conf
		_EOF
		)"
	fi
	HAVE_SET_DNS=1
}

unset_dns() {
	[[ ${#DNS[@]} -gt 0 ]] || return 0

	if [[ $(resolvconf --version 2>/dev/null) == openresolv\ * ]]; then
		cmd resolvconf -d "$INTERFACE"
	else
		cmd umount /etc/resolv.conf
	fi
}
