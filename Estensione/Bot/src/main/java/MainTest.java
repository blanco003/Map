

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import telegram.bot.MapBot;

/**
 * Classe di avvio del Bot.
 */
public class MainTest {

    /**
     * Registra il bot su telegram e lo rende pronto a rispondere alle richieste degli utenti.
     * @param args
     */
    public static void main(String[] args) {
    
        try{
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            String Token = "7172711483:AAE4-J_ZzYQV5CS6SKeFC0xSHV7rwBnJOZY";
            String Username = "map_progetto_bot";
            botsApi.registerBot(new MapBot(Token,Username));
        }catch(TelegramApiException e){
            System.out.println(e.getMessage());
        }
    }
}



