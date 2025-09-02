import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class BlockchainDemo {

    // ======== Block ========
    public static class Block {
        public final int index;
        public final long timestamp;
        public final String data;
        public final String previousHash;
        public int nonce;
        public String hash;

        public Block(int index, String data, String previousHash) {
            this.index = index;
            this.data = data;
            this.previousHash = previousHash;
            this.timestamp = System.currentTimeMillis();
            this.nonce = 0;
            this.hash = calculateHash();
        }

        public String calculateHash() {
            String input = index + previousHash + timestamp + data + nonce;
            return applySha256(input);
        }

        public void mineBlock(int difficulty) {
            String target = repeat('0', difficulty);
            while (!hash.startsWith(target)) {
                nonce++;
                hash = calculateHash();
            }
            System.out.println("✓ Block mined: " + hash);
        }

        private static String applySha256(String input) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(input.getBytes("UTF-8"));
                StringBuilder hex = new StringBuilder();
                for (byte b : hash) {
                    String h = Integer.toHexString(0xff & b);
                    if (h.length() == 1) hex.append('0');
                    hex.append(h);
                }
                return hex.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static String repeat(char c, int n) {
            StringBuilder sb = new StringBuilder(n);
            for (int i = 0; i < n; i++) sb.append(c);
            return sb.toString();
        }

        @Override
        public String toString() {
            return "Block{" +
                    "index=" + index +
                    ", timestamp=" + timestamp +
                    ", data='" + data + '\'' +
                    ", previousHash='" + previousHash + '\'' +
                    ", nonce=" + nonce +
                    ", hash='" + hash + '\'' +
                    '}';
        }
    }

    // Blockchain 
    public static class Blockchain {
        private final List<Block> chain = new ArrayList<>();
        private int difficulty = 4; // increase for slower mining

        public Blockchain() {
            chain.add(createGenesisBlock());
        }

        private Block createGenesisBlock() {
            System.out.println("Creating genesis block...");
            Block genesis = new Block(0, "Genesis Block", "0");
            genesis.mineBlock(difficulty);
            return genesis;
        }

        public Block getLatestBlock() {
            return chain.get(chain.size() - 1);
        }

        public void addBlock(String data) {
            Block previous = getLatestBlock();
            Block newBlock = new Block(previous.index + 1, data, previous.hash);
            System.out.println("Mining block " + newBlock.index + "...");
            newBlock.mineBlock(difficulty);
            chain.add(newBlock);
        }

        public boolean isChainValid() {
            for (int i = 1; i < chain.size(); i++) {
                Block current = chain.get(i);
                Block previous = chain.get(i - 1);

                // Recalculate hash and compare
                if (!current.hash.equals(current.calculateHash())) {
                    System.out.println("Invalid hash at block " + current.index);
                    return false;
                }
                // Check link
                if (!current.previousHash.equals(previous.hash)) {
                    System.out.println("Broken chain at block " + current.index);
                    return false;
                }
                // Check PoW
                String target = "0".repeat(getDifficulty());
                if (!current.hash.startsWith(target)) {
                    System.out.println("Block " + current.index + " not properly mined");
                    return false;
                }
            }
            return true;
        }

        public int size() {
            return chain.size();
        }

        public int getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(int difficulty) {
            if (difficulty < 0 || difficulty > 8) throw new IllegalArgumentException("difficulty 0..8");
            this.difficulty = difficulty;
        }

        public List<Block> getBlocks() {
            return List.copyOf(chain);
        }
    }

    // ======== Demo main ========
    public static void main(String[] args) {
        Blockchain bc = new Blockchain();
        // Optional: adjust difficulty (higher = slower)
        // bc.setDifficulty(5);

        bc.addBlock("Alice pays Bob 10");
        bc.addBlock("Bob pays Carol 3");
        bc.addBlock("Carol pays Dave 1");

        System.out.println("\nBlockchain has " + bc.size() + " blocks.");
        System.out.println("Chain valid? " + bc.isChainValid());

        System.out.println("\n-- Blocks --");
        for (Block b : bc.getBlocks()) {
            System.out.println(b);
        }

        // Tamper test: uncomment to see validation fail
        // bc.getBlocks().get(1).data = "Alice pays Bob 1000"; // won't compile: list is unmodifiable
        // (If you want to test tampering, expose a mutable accessor or change design accordingly)
    }
}
