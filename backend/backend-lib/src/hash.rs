use std::fs::File;
use std::io::{Read, self};
use std::path::Path;

use anyhow::Result;
use img_hash::{HasherConfig, HashAlg};
use sha2::{Sha256, Digest};

pub fn chash(path: impl AsRef<Path>) -> Result<[u8; 32]> {
    let mut file = File::open(path)?;

    let mut sha256 = Sha256::new();
    io::copy(&mut file, &mut sha256);

    Ok(sha256.finalize()[..].try_into().unwrap())
}

pub fn phash(path: impl AsRef<Path>) -> Result<[u8; 32]> {
    let image = image::open(path)?;
    let hasher = HasherConfig::new()
        .hash_alg(HashAlg::Gradient)
        .preproc_dct()
        .hash_size(16, 16) // 16 * 16 = 256 bits = 32 bytes
        .to_hasher();

    let hash = hasher.hash_image(&image);

    Ok(*(<&[u8] as TryInto<&[u8; 32]>>::try_into(hash.as_bytes()).unwrap()))
}
