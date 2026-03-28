import java.util.*;
/**
 * This represents a player in the game.
 * 
 * A player has a name, a turn order, a list of purchased cards 
 * and reserved cards, gems, and a list of nobles that a player claims.
 * 
 * A player can purchase cards, reserve cards, add or deduct gems,
 * and claim nobles during gameplay.
 * 
 */
public class Player {

    /**
     * The name of the player.
     */
    private String name;
    
    /**
     * The player's turn order in the game.
     */
    private int turnOrder;

    /**
     * The list of cards purchased by the player.
     */
    private List<Card> purchasedCards;

    /**
     * The list of cards reserved by the player.
     */
    private List<Card> reservedCards;

    /**
     * The gem collection currently owned by the player.
     */
    private GemCollection gems;

    /**
     * The list of nobles claimed by the player.
     */
    private List<Noble> claimedNobles;

    /**
     * Constructs a new player with the specified name and turn order.
     * 
     * @param name the name of the player
     * @param turnOrder the turn order of the player
     */
    public Player(String name, int turnOrder) {
        this.name = name;
        this.turnOrder = turnOrder;
        this.purchasedCards = new ArrayList<>();
        this.reservedCards = new ArrayList<>();
        this.claimedNobles = new ArrayList<>();
        this.gems = new GemCollection(); 
    }

    /**
     * Returns the name of the player.
     * 
     * @return the player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the player's turn order.
     * 
     * @return the turn order
     */
    public int getTurnOrder() {
        return turnOrder;
    }

    /**
     * Returns the list of cards purchased by the player.
     * 
     * @return the list of purchased cards
     */
    public List<Card> getPurchasedCards() {
        return purchasedCards;
    }

    /**
     * Returns the list of reserved cards owned by the player.
     * 
     * @return the list of reserved cards
     */
    public List<Card> getReservedCards() {
        return reservedCards;
    }

    /**
     * Returns the player's gem collection.
     * 
     * @return the player's gems
     */
    public GemCollection getGems() {
        return gems;
    }

    /**
     * Returns the number of gems of a specified color that the player owns.
     * 
     * @param color the gem color to check
     * @return the number of gems of the specified color
     */
    public int getSpecificGem(GemColor color) {
        return gems.getCount(color);
    }

    /**
     * Returns the list of nobles that the player claimed.
     * 
     * @return the list of claimed nobles
     */
    public List<Noble> getClaimedNobles() {
        return claimedNobles;
    }

    /**
     * Adds a purchased card to the player's collection.
     * 
     * @param card the card to be added
     */
    public void addCard(Card card) {
        this.purchasedCards.add(card);
    }

    /**
     * Adds a reserved card to the player's list of reserved cards.
     * 
     * @param card the card to reserve
     */
    public void addReservedCard(Card card) {
        this.reservedCards.add(card);
    }

    /**
     * Removes a reserved card from the player's list of reserved cards.
     * 
     * @param card the card to reserve
     */
    public void removeReservedCard(Card card) {
        this.reservedCards.remove(card); 
    }

    /**
     * Adds the specified gem collection to the player's existing gems.
     * 
     * @param newGems the gems to add
     */
    public void addGems(GemCollection newGems) {
        gems.add(newGems);
    }

    /**
     * Deducts the specified gem collection from the player's existing gems.
     * 
     * @param gems the gems to deduct
     */
    public void deductGems(GemCollection spentGems) {
        gems.subtract(spentGems);
    }

    /**
     * Adds the specified noble to the player's list of claimed nobles.
     * 
     * @param noble the noble to claim
     */
    public void claimNoble(Noble noble) {
        this.claimedNobles.add(noble);
    }
    
    /**
     * Returns the total number of gems owned by the player.
     * 
     * @return the total gem count
     */
    public int getGemCount() {
        // to test if its over the limit of 10
        return gems.getTotalCount();
    }

    /**
     * Calculates and returns the total number of points 
     * earned by the player from purchased cards and claimed nobles.
     * 
     * @return the total points of the player
     */
    public int getPoints() {
        int totalPoints = 0;
        for (Card c:purchasedCards) {
            totalPoints += c.getPoints();
        }
        for (Noble n:claimedNobles) {
            totalPoints += n.getPoints();
        }
        return totalPoints;
    }

    /**
     * Calculates the permament gem bonuses gained from the player's purchased cards.
     * 
     * @return a map containing each gem color and its corresponding bonus count
     */
    public Map<GemColor, Integer> calculateBonuses() {
        // Iterate through purchasedCards, get their gem colors, and tally them 
        GemCollection gems = new GemCollection();
        for (Card c : purchasedCards) {
            GemColor gem = c.getBonus();
            gems.add(gem, 1);
        }
        return gems.getGems();
    }

    /**
     * Packages the player's score, tokens, and bonuses into a string.
     * Example output: "SCORE-15,TOKENS:[DIAMOND-2,ONYX-1...],BONUSES:[RUBY-3,EMERALD-1...]"
     */
    public String getPlayerStateAsString() {
        StringBuilder sb = new StringBuilder();
        
        // 1. Add the player's Prestige Points (Score)
        sb.append("SCORE-").append(getPoints()).append(",");
        
        // 2. Add the player's physical tokens in hand
        sb.append("TOKENS:[").append(gems.getBankAsString()).append("],");
        
        // 3. Add the player's permanent card bonuses
        GemCollection temporaryBonuses = new GemCollection();
        for (Card c : purchasedCards) {
            temporaryBonuses.add(c.getBonus(), 1);
        }
        sb.append("BONUSES:[").append(temporaryBonuses.getBankAsString()).append("]");

        sb.append(",RESERVED:[");
        if (reservedCards != null && !reservedCards.isEmpty()) {
            for (Card c : reservedCards) {
                // Formatting as COLOR-POINTS (e.g., RUBY-2)
                sb.append(c.getBonus().name()).append("-").append(c.getPoints()).append(",");
            }
            // Strip off the final comma if we added any cards
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setLength(sb.length() - 1);
            }
        } else {
            sb.append("NONE");
        }
        sb.append("]");
        return sb.toString();
    }

    
}