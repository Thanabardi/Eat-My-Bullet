public class CommandTurnSouthWest extends Command{

    public CommandTurnSouthWest(Player player) {
        super(player);
    }

    @Override
    public void execute() {
        getPlayer().turnSouthWest();
    }
}
