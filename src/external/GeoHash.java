package external;

// A copy from http://developer-should-know.com/post/87283491372/geohash-encoding-and-decoding-algorithm
//这个class的目的是为了将经纬度lat lon 转化为TickerMaster API现在所识别的geoPoint. 这是网上现成的转换方式
public class GeoHash {

    private static final String BASE_32 = "0123456789bcdefghjkmnpqrstuvwxyz";  //Base_32是转换经纬度值得编码方式

    private static int divideRangeByValue(double value, double[] range) {
        double mid = middle(range);
        if (value >= mid) {
            range[0] = mid;
            return 1;
        } else {
            range[1] = mid;
            return 0;
        }
    }

    private static double middle(double[] range) {
        return (range[0] + range[1]) / 2;
    }

    public static String encodeGeohash(double latitude, double longitude, int precision) { //具体的实现是依据这个函数 将经纬度和精确度三个标准传进来 然后返回一个String 这个String就是我们所需要的哈希值
        double[] latRange = new double[]{-90.0, 90.0};
        double[] lonRange = new double[]{-180.0, 180.0};
        boolean isEven = true;
        int bit = 0;
        int base32CharIndex = 0;
        StringBuilder geohash = new StringBuilder();

        while (geohash.length() < precision) {
            if (isEven) {
                base32CharIndex = (base32CharIndex << 1) | divideRangeByValue(longitude, lonRange);
            } else {
                base32CharIndex = (base32CharIndex << 1) | divideRangeByValue(latitude, latRange);
            }

            isEven = !isEven;

            if (bit < 4) {
                bit++;
            } else {
                geohash.append(BASE_32.charAt(base32CharIndex));
                bit = 0;
                base32CharIndex = 0;
            }
        }

        return geohash.toString();
    }

    public static void main(String[] args) {
    	// Expect to see 'u4pruydqqvj8'
    	System.out.println(encodeGeohash(57.64911, 10.40744, 8));  // 所给的精确不不一样 所得结果的长度也不一样 现在 8位精确度时 就到u4pruydq 12位时才是上面的结果
    }
}
