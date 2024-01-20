use std::fs::File;
use std::io::{Read, self};
use std::path::Path;

use anyhow::Result;
use sha2::{Sha256, Digest};
// use opencv::imgcodecs::{imread, IMREAD_COLOR};
// use opencv::img_hash::p_hash;
// use opencv::core::Vector;

pub fn chash(path: impl AsRef<Path>) -> Result<[u8; 32]> {
    let mut file = File::open(path)?;

    let mut sha256 = Sha256::new();
    io::copy(&mut file, &mut sha256);

    Ok(sha256.finalize()[..].try_into().unwrap())
}

// pub fn phash(path: impl AsRef<Path>) -> Result<[u8; 8]> {
//     let path_str = path.as_ref().as_os_str().to_str().unwrap();
//     let image = imread(path_str, IMREAD_COLOR)?;

//     let mut hash: Vector<u8> = Vector::new();
//     p_hash(&image, &mut hash);

//     Ok(hash.to_vec()[..].try_into().unwrap())
// }
