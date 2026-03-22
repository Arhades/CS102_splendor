import java.util.*;

public class Player {
    private String name;
    private int turnOrder;
    private List<Card> purchasedCards;
    private List<Card> reservedCards;
    private GemCollection gems;
    private List<Noble> claimedNobles;

    public Player(String name, int turnOrder) {
        this.name = name;
        this.turnOrder = turnOrder;
        this.purchasedCards = new ArrayList<>();
        this.reservedCards = new ArrayList<>();
        this.claimedNobles = new ArrayList<>();
        this.gems = new GemCollection(); 
    }

    public String getName() {
        return name;
    }

    public int getTurnOrder() {
        return turnOrder;
    }

    public List<Card> getPurchasedCards() {
        return purchasedCards;
    }

    public List<Card> getReservedCards() {
        return reservedCards;
    }

    public GemCollection getGems() {
        return gems;
    }

    public int getSpecificGem(GemColor color) {
        return gems.getCount(color);
    }

    public List<Noble> getClaimedNobles() {
        return claimedNobles;
    }

    public void addCard(Card card) {
        this.purchasedCards.add(card);
    }

    public void addReservedCard(Card card) {
        this.reservedCards.add(card);
    }

    public void removeReservedCard(Card card) {
        this.reservedCards.remove(card); 
    }

    public void claimNoble(Noble noble) {
        this.claimedNobles.add(noble);
    }
    
    public int getGemCount() {
        // to test if its over the limit of 10
        return gems.getTotalCount();
    }

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

    public Map<GemColor, Integer> calculateBonuses() {
        // Iterate through purchasedCards, get their gem colors, and tally them 
        GemCollection gems = new GemCollection();
        for (Card c : purchasedCards) {
            GemColor gem = c.getBonus();
            gems.add(gem, 1);
        }
        return gems.getGems();
    }

    public void addGems(GemCollection newGems) {
        gems.add(newGems);
    }

    public void deductGems(GemCollection spentGems) {
        gems.subtract(spentGems);
    }
}