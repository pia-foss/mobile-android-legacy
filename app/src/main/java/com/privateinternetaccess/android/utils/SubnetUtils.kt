package com.privateinternetaccess.android.utils

object SubnetUtils {

    fun excludeIpFromSubnet(subnet: String, excludeIp: String): String {
        val (baseIp, cidr) = subnet.split('/')
        val start = ipToInt(baseIp)
        val size = 1 shl (32 - cidr.toInt())
        val end = start + size - 1
        val excludeIpInt = ipToInt(excludeIp)

        // Quick check if the IP to exclude is part of the subnet
        if (!isIpInSubnet(excludeIpInt, start, size)) {
            return subnet
        }

        val results = mutableListOf<String>()

        var currentStart = start
        while (currentStart <= end) {
            var mask = 32
            while ((currentStart and (1 shl (32 - mask))) == 0 &&
                (currentStart + (1 shl (32 - mask)) - 1) <= end) {
                mask--
            }
            val currentEnd = currentStart + (1 shl (32 - mask)) - 1

            // If the excluded IP falls within this subnet, adjust the boundaries
            if (excludeIpInt in currentStart..currentEnd) {
                if (currentStart != excludeIpInt) {
                    results.add("${intToIp(currentStart)}/${32 - Integer.numberOfTrailingZeros(excludeIpInt - currentStart)}")
                }
                if (currentEnd != excludeIpInt) {
                    results.add("${intToIp(excludeIpInt + 1)}/${32 - Integer.numberOfTrailingZeros(currentEnd - excludeIpInt)}")
                }
                break
            } else {
                results.add("${intToIp(currentStart)}/$mask")
            }

            currentStart = currentEnd + 1
        }

        return results.joinToString(",")
    }

    private fun isIpInSubnet(ip: Int, subnetStart: Int, size: Int): Boolean {
        return ip >= subnetStart && ip < subnetStart + size
    }

    private fun ipToInt(ip: String): Int {
        return ip.split('.').fold(0) { acc, part ->
            (acc shl 8) or part.toInt()
        }
    }

    private fun intToIp(intValue: Int): String {
        return "${(intValue shr 24) and 0xFF}.${(intValue shr 16) and 0xFF}.${(intValue shr 8) and 0xFF}.${intValue and 0xFF}"
    }
}