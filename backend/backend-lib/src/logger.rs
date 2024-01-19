use std::sync::OnceLock;
use tokio::sync::mpsc;

pub struct Logger {
    log_channel: mpsc::UnboundedSender<(log::Level, Box<str>)>,
}

static LOGGER: OnceLock<Logger> = OnceLock::new();

impl Logger {
    pub fn init(log_channel: mpsc::UnboundedSender<(log::Level, Box<str>)>) {
        LOGGER.set({
            Logger { log_channel }
        })
        .or_else::<(), _>(|_| panic!("Logger::init can only be called once"))
        .unwrap();

        log::set_logger(LOGGER.get().unwrap()).expect("Failed to set logger");
    }
}

impl log::Log for Logger {
    fn enabled(&self, metadata: &log::Metadata) -> bool {
        true
    }

    fn log(&self, record: &log::Record) {
        let message = record.args().to_string().into_boxed_str();
        self.log_channel.send((record.level(), message))
            .expect("Logger was closed");
    }

    fn flush(&self) {}
}
