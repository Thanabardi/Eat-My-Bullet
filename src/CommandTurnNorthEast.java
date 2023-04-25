public class CommandTurnNorthEast extends Command{

    public CommandTurnNorthEast(Player player) {
        super(player);
    }

    @Override
    public void execute() {
        getPlayer().turnNorthEast();
    }
}
