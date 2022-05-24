package com.gerege.verifoncardreader.pos;


import static java.nio.charset.StandardCharsets.UTF_8;

import android.util.Base64;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RsaUtils {

    private static PublicKey getPublicKey() {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            String s;
            switch (BankMode.getBank()) {
                case TDB_BANK:
                    s = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0UtfyZwyCvlPJ6fr81nnb5kktMtsjSdu8Twg/ZhHXP+KqnVeotNhdPKCZRzesN5ZPx6EBFtQwomkEJ9u4V80WSNRQbJEbeR8qVC91VCiBz3W85zwOXcMksQXL+YRw9uTGHCpjEwDuCpmW4vX5eEYZotVKIdpt9I1KAT40k5zH/aANSKvMiyTXWsuBGHmePvL2dWlAQ+qE9PnWLqs55SOjWGA2lzsIo400vMa7lCzvuQpwsAwCz6opTQB6rD/+w/nvoFPCy2yozUT9quoBl2jLu88JaYAcNn2AS9vZctknJzs97hC/OMcQXqsEiTcgqAsf+a/+yY4ay2IIm0cEiIrZQIDAQAB";
                    break;
                case GOLOMT_BANK:
                    s = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp2G00vhrrTyvbO03njyuu26KGtvVKjsA7E+v9CbyqmaiJKrqzyDUSEq+K5xczelX/EWS4tIODgKKgwkx1AfWBC6dy/IBs13zSW0wb83XpEZvLiLpXXVDORyrIQbLZeiq9qyQbCQISpTrO6Lv3cOpyVIW4ng+Rag2n48BX8MvJMwzqSybHU6KcmOUD0lOesq4nmDwNvmO9FMGCgDOxicE8FZ8Rs7RnlHsUfrGW+Cu5+U5L2lB0uyGg1Kc70LsLSEIiThrnW+750KI+Mm9dDZvur3JWtwzvSDCQO5UL/LBb913cKxKtfSVXmYN060Gc+jDxMFPHpnyaLoG2GQpPMteXwIDAQAB";
                    break;
                case STATE_BANK:
                    s = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0UtfyZwyCvlPJ6fr81nnb5kktMtsjSdu8Twg/ZhHXP+KqnVeotNhdPKCZRzesN5ZPx6EBFtQwomkEJ9u4V80WSNRQbJEbeR8qVC91VCiBz3W85zwOXcMksQXL+YRw9uTGHCpjEwDuCpmW4vX5eEYZotVKIdpt9I1KAT40k5zH/aANSKvMiyTXWsuBGHmePvL2dWlAQ+qE9PnWLqs55SOjWGA2lzsIo400vMa7lCzvuQpwsAwCz6opTQB6rD/+w/nvoFPCy2yozUT9quoBl2jLu88JaYAcNn2AS9vZctknJzs97hC/OMcQXqsEiTcgqAsf+a/+yY4ay2IIm0cEiIrZQIDAQAB";
                    break;
                default:
                    s = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0UtfyZwyCvlPJ6fr81nnb5kktMtsjSdu8Twg/ZhHXP+KqnVeotNhdPKCZRzesN5ZPx6EBFtQwomkEJ9u4V80WSNRQbJEbeR8qVC91VCiBz3W85zwOXcMksQXL+YRw9uTGHCpjEwDuCpmW4vX5eEYZotVKIdpt9I1KAT40k5zH/aANSKvMiyTXWsuBGHmePvL2dWlAQ+qE9PnWLqs55SOjWGA2lzsIo400vMa7lCzvuQpwsAwCz6opTQB6rD/+w/nvoFPCy2yozUT9quoBl2jLu88JaYAcNn2AS9vZctknJzs97hC/OMcQXqsEiTcgqAsf+a/+yY4ay2IIm0cEiIrZQIDAQAB";
                    break;
            }
//            String s = BankMode.isTDB()
//                    ? "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0UtfyZwyCvlPJ6fr81nnb5kktMtsjSdu8Twg/ZhHXP+KqnVeotNhdPKCZRzesN5ZPx6EBFtQwomkEJ9u4V80WSNRQbJEbeR8qVC91VCiBz3W85zwOXcMksQXL+YRw9uTGHCpjEwDuCpmW4vX5eEYZotVKIdpt9I1KAT40k5zH/aANSKvMiyTXWsuBGHmePvL2dWlAQ+qE9PnWLqs55SOjWGA2lzsIo400vMa7lCzvuQpwsAwCz6opTQB6rD/+w/nvoFPCy2yozUT9quoBl2jLu88JaYAcNn2AS9vZctknJzs97hC/OMcQXqsEiTcgqAsf+a/+yY4ay2IIm0cEiIrZQIDAQAB"
//                    : "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAp2G00vhrrTyvbO03njyuu26KGtvVKjsA7E+v9CbyqmaiJKrqzyDUSEq+K5xczelX/EWS4tIODgKKgwkx1AfWBC6dy/IBs13zSW0wb83XpEZvLiLpXXVDORyrIQbLZeiq9qyQbCQISpTrO6Lv3cOpyVIW4ng+Rag2n48BX8MvJMwzqSybHU6KcmOUD0lOesq4nmDwNvmO9FMGCgDOxicE8FZ8Rs7RnlHsUfrGW+Cu5+U5L2lB0uyGg1Kc70LsLSEIiThrnW+750KI+Mm9dDZvur3JWtwzvSDCQO5UL/LBb913cKxKtfSVXmYN060Gc+jDxMFPHpnyaLoG2GQpPMteXwIDAQAB";

            X509EncodedKeySpec keySpecPKCS8Pub = new X509EncodedKeySpec(Base64.decode(s, Base64.DEFAULT));
            return kf.generatePublic(keySpecPKCS8Pub);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PrivateKey getPrivateKey() {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");

            String s;
            switch (BankMode.getBank()) {
                case TDB_BANK:
                    s = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCB1Nfd95mFTbWpRHeLEzi1bBpCZs0prhgjOPjRuw7w3mfuBIuH8vLv7gcaNFDT7Q0bP2c312rSrDZFT0XKip8TNSJsaj26mzeuDwVQgwSYfY/nscSSKncm/Y1apUJOMMdEWDZT2+/7odeSQcspSLM/38QOEx8uEVR3sIMERlMgiIyaVBfrRmxUWr5WtoQcdwVmK1jPxORsdDsrcJDZgJOeXkgnhHJCwiY37s7rgaFhTpIxvoPrBeydTSlAZwTu6Krxvn/8qdehWj/++k5b5aya+aq2YHzCX5J50DLF8vzBsj/edM8Jjg1e3aFji1dEO5e4Hd7i1qbT3Zaee9bEkK4xAgMBAAECggEARFcGHefUHrAN/IB2sHU4t36kv+ke+16RhBDcIIHnVnCrokUwMSp4KEVR5cA9/5/a80nHikRRDX3qv0cABKI9I74nVYwC3DQR9gQgFwYwKBiH5DzVrN6/g32Zp2ov4bqZWWdZiYDO7NR/BZSuXRfQisG9kG8OTZB/ttTp0hk2bnvP6vOE0YtYB+m5VB/8hT4M9ZN/wvLEfJPXgp+fmQ4FdScqIFJeaZoKDULddnrZCbZFVA20VjDkw0gbxuKXb2rFvZkosSxW2GqYZG+u8PZxpfI6FoHLOTj/Cfeu3JWWGn28MgMavQafzkv7rVbJ2DKpn6z1AFwa6SpQkk3cWNFFcQKBgQDUKjkwenu0te/UQi0TAeebMpsSu4LBVVz7nN/5SS6zlIASBmuc05qH8/kVjkuZPDIlMvdIMEivJiOjBegio6/X6oocdrrMrwZNWVOCFeAFfw4LyBbeIoTVLEKjbAoj1IQCMGwb3d3XDND2d0O8lDNu0jMBLNn8wOsxLGyTeg6pLQKBgQCcp9r3jQyxSW9V4Lqfe58uzpktJh3aVxgMkG8T1Gb9x8XFsOshUiaAD15Jf5Ugk/SLnJHBUmgV0Gos0jTdfClX1Jg4b0y3kv7NK86BKzrKICTSNiFqPWvpzJRFDukHHSLK8lWEDd5KPw2vPVPkVyt6MSOfrzIun+9/I8AuN81zlQKBgBVXKel2aHsE0drHzUDcMC+sJ2z672wC6hdk8CJi+g+WIEuv2aMspZ6HK8a/SVtgcqrLNZRNRwJXE76SGYdib42ISdwTFTcBY+Dj20J6cM4cuYz101cEodfPW+SUOXDhL2YI+NbUujunTl18IZQWVBYRjo/Psx15Si7oCQPMgy0BAoGAMjOwfihbau0HmtO7uqQZLRzUBZ3zbyb98/jNP+OPZcCBZor9rt3urJdANfmGxrvjYGI6a2OHK4ZwoBB1FI1Ximf8qAky4plewk1JjidWXbPTWPIz3yHz1SbUVRUkJD14CuWB9iGjnNN8BS54FfLBYhUhP2kbgcmLLIk+axFC68kCgYBsMXyy0cg8mKppI5IlWYSve1MUgXq63QKs7+kxYUbQN7Xdh1eWYxFBRMwCJn1pakK/Wl9leMd+4lfMmbH4XxV2n0DkUN+v4BYnnx/lOf5TCnCMzHZQh1I8h1PiY7WPtso6XQiJIMGGpL8fojXNUHqsvpgfsm5zcztuUFXAgf+vlA==";
                    break;
                case GOLOMT_BANK:
                    s = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCyI8fIfVFDHLv/eSKrAaKM/x2DyWRKeYt7PuEd85oF1DEt0VfsEayPHRFIqgAlsJG1PJfkaO+UyVrL9HO6k6RoAi90SuHpIpug76QsobSMpBVET6fRhPSKt7AF5jFvNrC3X7PENX4kKyxHPFd1AiQdDxH5YDH+j6nGmKmkdPcURuCTjgyCsTdt3pLLT5ZappEeBNofSIQxOUYWXCdAo20fp1vR8kYadwua8/SuvLO3YjtZJkh63NS9aKFdNDpiB1PoY96cdThF73ItvU5NiXTKn/xuUclWIq4dvoJMCLkUSZCDYLk/Dd54qPwq3n0iG7ZAPHtb0B40Cj1d+E5Ie2hvAgMBAAECggEABkjM233QSBXTbAfR+UZtfZviZqvMJfRzvz1Qo8XcQShjM8KW33UgKUxVN8MHjkzUVxh0I1KwYQdZdKiDDKn4utKY6flaTucxplPN7uUlgXCob2h/+xw9pEcmpo6gX9QGF/IAXYOll5EOVtUCINaaf4vwjmOeX+ShkKgCFBMsRELMmA1OPjb6gzJ3MuBkuEr3WiglVhqp9vwm9mFMQv7JTPU848fief/VoK81wWFUnWSCo7MI/X27ZvCVV4xoljR6FCDBDUgIzTA325kxG24VXxlpJJc7GBUfZCbI5n6JDx2Z2hWaID+v9yLQz3EP6fXwJ90i0c6yJyb3OzTH1XNkgQKBgQD6CwfhyaoeYLgc/EP24oMFLDsSgBagjpGZA4D/9z9U8QW0K6brMPViQ0BiO3H8Fp9MiovEcUQP6JhcTy4Tm+70fF5VrE7lO6F3UPUM/mBwy6Yd2DewHE6IGHGCx/xCbrk/0AdisEi9P9/A3R6enmHlFVn5AzZhChMbmh7UJI8QGQKBgQC2YjlKOOR4enCaa+EmCXJsX69C8ZNSLe+nenemXlAhgvIPLUuMTx594VpHF0S9FZBwHNAH9yLbWFvBDdSH7IKjkAa7pZ8B2luYHVSUEFtrD/D8ChoiKPlpM5CldtLvYJ5hkNzbqupPahVq4z6coTINpW1I4p6dg08BoqFVky2txwKBgA5TkXkLX/brncDzZ0V/BvjF5wCRXDqI0KjOvZZs6TEJukTvd1nkbTrbFZZLR6Wk+i2dhpKT1s1+izRGySIQfil98r2/6KlTuFWoytGaxZXQ5UyydcZeaNV9aQuQP9DinTSdy76I0TIetchMIxP9iShiB8yvtW7r79pWA0wuaBAJAoGBAKKEOASZWYy5nekcRA3+0QUc5jFCzkGmSSz1hfSrib+yzTsU44LtZYBJ9IXwiAHtWz1rjzvpzheiTnBigIV2DJru1fETjfK6vm93g0xEnxFxrQ0Je2wBEu3ZUwT+Msu3TqolyYe8bvnAz8DlKX+sa8uZIx6VQT2eKiKM5jI46bU1AoGBAMCJsRcMG/e9tYfcxhcnQ/ThNg+VV+zsSkGfuGAQpfRFcf4Mfy/AqFVVDCQ6GKZdm5WcWj452AB6Ts3pbV9m4AUBImpBFvosYEdbnbJE7m27IFScJ1ft/X2k1D9RmdlxbwSiI0zrStPDC6z5zrZMYv0bL4iXJGn/nBx0YmAqIYWw";
                    break;
                case STATE_BANK:
                    s = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCB1Nfd95mFTbWpRHeLEzi1bBpCZs0prhgjOPjRuw7w3mfuBIuH8vLv7gcaNFDT7Q0bP2c312rSrDZFT0XKip8TNSJsaj26mzeuDwVQgwSYfY/nscSSKncm/Y1apUJOMMdEWDZT2+/7odeSQcspSLM/38QOEx8uEVR3sIMERlMgiIyaVBfrRmxUWr5WtoQcdwVmK1jPxORsdDsrcJDZgJOeXkgnhHJCwiY37s7rgaFhTpIxvoPrBeydTSlAZwTu6Krxvn/8qdehWj/++k5b5aya+aq2YHzCX5J50DLF8vzBsj/edM8Jjg1e3aFji1dEO5e4Hd7i1qbT3Zaee9bEkK4xAgMBAAECggEARFcGHefUHrAN/IB2sHU4t36kv+ke+16RhBDcIIHnVnCrokUwMSp4KEVR5cA9/5/a80nHikRRDX3qv0cABKI9I74nVYwC3DQR9gQgFwYwKBiH5DzVrN6/g32Zp2ov4bqZWWdZiYDO7NR/BZSuXRfQisG9kG8OTZB/ttTp0hk2bnvP6vOE0YtYB+m5VB/8hT4M9ZN/wvLEfJPXgp+fmQ4FdScqIFJeaZoKDULddnrZCbZFVA20VjDkw0gbxuKXb2rFvZkosSxW2GqYZG+u8PZxpfI6FoHLOTj/Cfeu3JWWGn28MgMavQafzkv7rVbJ2DKpn6z1AFwa6SpQkk3cWNFFcQKBgQDUKjkwenu0te/UQi0TAeebMpsSu4LBVVz7nN/5SS6zlIASBmuc05qH8/kVjkuZPDIlMvdIMEivJiOjBegio6/X6oocdrrMrwZNWVOCFeAFfw4LyBbeIoTVLEKjbAoj1IQCMGwb3d3XDND2d0O8lDNu0jMBLNn8wOsxLGyTeg6pLQKBgQCcp9r3jQyxSW9V4Lqfe58uzpktJh3aVxgMkG8T1Gb9x8XFsOshUiaAD15Jf5Ugk/SLnJHBUmgV0Gos0jTdfClX1Jg4b0y3kv7NK86BKzrKICTSNiFqPWvpzJRFDukHHSLK8lWEDd5KPw2vPVPkVyt6MSOfrzIun+9/I8AuN81zlQKBgBVXKel2aHsE0drHzUDcMC+sJ2z672wC6hdk8CJi+g+WIEuv2aMspZ6HK8a/SVtgcqrLNZRNRwJXE76SGYdib42ISdwTFTcBY+Dj20J6cM4cuYz101cEodfPW+SUOXDhL2YI+NbUujunTl18IZQWVBYRjo/Psx15Si7oCQPMgy0BAoGAMjOwfihbau0HmtO7uqQZLRzUBZ3zbyb98/jNP+OPZcCBZor9rt3urJdANfmGxrvjYGI6a2OHK4ZwoBB1FI1Ximf8qAky4plewk1JjidWXbPTWPIz3yHz1SbUVRUkJD14CuWB9iGjnNN8BS54FfLBYhUhP2kbgcmLLIk+axFC68kCgYBsMXyy0cg8mKppI5IlWYSve1MUgXq63QKs7+kxYUbQN7Xdh1eWYxFBRMwCJn1pakK/Wl9leMd+4lfMmbH4XxV2n0DkUN+v4BYnnx/lOf5TCnCMzHZQh1I8h1PiY7WPtso6XQiJIMGGpL8fojXNUHqsvpgfsm5zcztuUFXAgf+vlA==";
                    break;
                default:
                    s = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCB1Nfd95mFTbWpRHeLEzi1bBpCZs0prhgjOPjRuw7w3mfuBIuH8vLv7gcaNFDT7Q0bP2c312rSrDZFT0XKip8TNSJsaj26mzeuDwVQgwSYfY/nscSSKncm/Y1apUJOMMdEWDZT2+/7odeSQcspSLM/38QOEx8uEVR3sIMERlMgiIyaVBfrRmxUWr5WtoQcdwVmK1jPxORsdDsrcJDZgJOeXkgnhHJCwiY37s7rgaFhTpIxvoPrBeydTSlAZwTu6Krxvn/8qdehWj/++k5b5aya+aq2YHzCX5J50DLF8vzBsj/edM8Jjg1e3aFji1dEO5e4Hd7i1qbT3Zaee9bEkK4xAgMBAAECggEARFcGHefUHrAN/IB2sHU4t36kv+ke+16RhBDcIIHnVnCrokUwMSp4KEVR5cA9/5/a80nHikRRDX3qv0cABKI9I74nVYwC3DQR9gQgFwYwKBiH5DzVrN6/g32Zp2ov4bqZWWdZiYDO7NR/BZSuXRfQisG9kG8OTZB/ttTp0hk2bnvP6vOE0YtYB+m5VB/8hT4M9ZN/wvLEfJPXgp+fmQ4FdScqIFJeaZoKDULddnrZCbZFVA20VjDkw0gbxuKXb2rFvZkosSxW2GqYZG+u8PZxpfI6FoHLOTj/Cfeu3JWWGn28MgMavQafzkv7rVbJ2DKpn6z1AFwa6SpQkk3cWNFFcQKBgQDUKjkwenu0te/UQi0TAeebMpsSu4LBVVz7nN/5SS6zlIASBmuc05qH8/kVjkuZPDIlMvdIMEivJiOjBegio6/X6oocdrrMrwZNWVOCFeAFfw4LyBbeIoTVLEKjbAoj1IQCMGwb3d3XDND2d0O8lDNu0jMBLNn8wOsxLGyTeg6pLQKBgQCcp9r3jQyxSW9V4Lqfe58uzpktJh3aVxgMkG8T1Gb9x8XFsOshUiaAD15Jf5Ugk/SLnJHBUmgV0Gos0jTdfClX1Jg4b0y3kv7NK86BKzrKICTSNiFqPWvpzJRFDukHHSLK8lWEDd5KPw2vPVPkVyt6MSOfrzIun+9/I8AuN81zlQKBgBVXKel2aHsE0drHzUDcMC+sJ2z672wC6hdk8CJi+g+WIEuv2aMspZ6HK8a/SVtgcqrLNZRNRwJXE76SGYdib42ISdwTFTcBY+Dj20J6cM4cuYz101cEodfPW+SUOXDhL2YI+NbUujunTl18IZQWVBYRjo/Psx15Si7oCQPMgy0BAoGAMjOwfihbau0HmtO7uqQZLRzUBZ3zbyb98/jNP+OPZcCBZor9rt3urJdANfmGxrvjYGI6a2OHK4ZwoBB1FI1Ximf8qAky4plewk1JjidWXbPTWPIz3yHz1SbUVRUkJD14CuWB9iGjnNN8BS54FfLBYhUhP2kbgcmLLIk+axFC68kCgYBsMXyy0cg8mKppI5IlWYSve1MUgXq63QKs7+kxYUbQN7Xdh1eWYxFBRMwCJn1pakK/Wl9leMd+4lfMmbH4XxV2n0DkUN+v4BYnnx/lOf5TCnCMzHZQh1I8h1PiY7WPtso6XQiJIMGGpL8fojXNUHqsvpgfsm5zcztuUFXAgf+vlA==";
                    break;
            }

            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(s, Base64.DEFAULT));
            return kf.generatePrivate(keySpecPKCS8);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encrypt(String plainText) throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, getPublicKey());

        byte[] cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8));

//        return Base64.getEncoder().encodeToString(cipherText);
        return Base64.encodeToString(cipherText, Base64.DEFAULT).replace("\n", "");
    }

    public static String decrypt(String cipherText) throws Exception {
//        byte[] bytes = Base64.getDecoder().decode(cipherText);
        byte[] bytes = Base64.decode(cipherText, Base64.DEFAULT);

        Cipher decriptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decriptCipher.init(Cipher.DECRYPT_MODE, getPrivateKey());

        return new String(decriptCipher.doFinal(bytes), UTF_8).replace("\n", "");
    }

    public static String sign(String plainText) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(getPrivateKey());
        privateSignature.update(plainText.getBytes(UTF_8));

        byte[] signature = privateSignature.sign();

//        return Base64.getEncoder().encodeToString(signature);
        return Base64.encodeToString(signature, Base64.DEFAULT).replace("\n", "");
    }

    public static boolean verify(String plainText, String signature) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(getPublicKey());
        publicSignature.update(plainText.getBytes(UTF_8));

//        byte[] signatureBytes = Base64.getDecoder().decode(signature);
        byte[] signatureBytes = Base64.decode(signature, Base64.DEFAULT);

        return publicSignature.verify(signatureBytes);
    }

}
