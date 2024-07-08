package telegram;

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
     */
    public static void main(String[] args) {
    
        try{
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new MapBot("7405128432:AAEYKtB8eXdS_Dt6EmJpI-hxoz4afcdUJ24", "spring_map_boot_bot"));
        }catch(TelegramApiException e){
            System.out.println(e.getMessage());
        }
    }
}



