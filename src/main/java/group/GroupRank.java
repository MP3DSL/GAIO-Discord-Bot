package group;

public enum GroupRank {
    USER("Regular user"),
    MOD("Moderator"),
    ADMIN("Group Administrator"),
    OWNER("Creator of this Discord Channel");
    private final String description;

    GroupRank(String description) {
        this.description = description;
    }

    /**
     * find a rank by name
     *
     * @param search the role to search for
     * @return rank || null
     */
    public static GroupRank findRank(String search) {
        for (GroupRank groupRank : values()) {
            if (groupRank.name().equalsIgnoreCase(search)) {
                return groupRank;
            }
        }
        return null;
    }

    public boolean isAtLeast(GroupRank rank) {
        return this.ordinal() >= rank.ordinal();
    }

    public boolean isHigherThan(GroupRank rank) {
        return this.ordinal() > rank.ordinal();
    }

    public String getDescription() {
        return description;
    }
}
