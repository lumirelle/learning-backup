package logger

import (
	"os"
	"time"

	"github.com/rs/zerolog"
)

// New 返回结构化日志器；dev 环境使用可读的 Console 格式。
func New(env string) zerolog.Logger {
	if env == "dev" {
		return zerolog.New(zerolog.ConsoleWriter{Out: os.Stdout, TimeFormat: time.RFC3339}).
			With().Timestamp().Logger()
	}
	return zerolog.New(os.Stdout).With().Timestamp().Logger()
}
