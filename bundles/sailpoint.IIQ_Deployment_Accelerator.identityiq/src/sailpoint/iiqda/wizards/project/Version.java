package sailpoint.iiqda.wizards.project;

/**
 * Created by brian.rose on 2/22/2016.
 * <p/>
 * Taken from
 * http://stackoverflow.com/questions/198431/how-do-you-compare-two-version-strings-in-java
 */
public class Version implements Comparable<Version> {

    private static final int[] PRIME = {2, 3, 5};
    private String version;

    public final String getVersion() {
        return this.version;
    }

    public final void setVersion(String version) {
        if (version != null) {
            if (!version.matches("[0-9]{1,2}(\\.[0-9]{1,4}){0,4}"))
                throw new IllegalArgumentException("Invalid version format");
        }

        this.version = version;
    }

    public Version() {
    }

    public Version(String version) {
        setVersion(version);
    }

    @Override
    public int compareTo(Version that) {
        if (that == null)
            return 1;
        String[] thisParts = this.version.split("\\.");
        String[] thatParts = that.version.split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            if (thisPart < thatPart)
                return -1;
            if (thisPart > thatPart)
                return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null)
            return false;
        if (this.getClass() != that.getClass())
            return false;
        return this.compareTo((Version) that) == 0;
    }

    @Override
    public final int hashCode() {
        final String[] parts = this.version.split("\\.");
        int hashCode = 0;
        for (int i = 0; i < parts.length; i++) {
            final int part = Integer.parseInt(parts[i]);
            if (part > 0) {
                hashCode += PRIME[i] ^ part;
            }
        }
        return hashCode;
    }

}