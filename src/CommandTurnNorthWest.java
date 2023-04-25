public class CommandTurnNorthWest extends Command{

    public CommandTurnNorthWest(Player player) {
        super(player);
    }

    @Override
    public void execute() {
        getPlayer().turnNorthWest();
    }
}
