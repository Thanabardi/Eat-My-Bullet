public class CommandTurnSouthEast extends Command{

    public CommandTurnSouthEast(Player player) {
        super(player);
    }

    @Override
    public void execute() {
        getPlayer().turnSouthEast();
    }
}
