use std::fs::File;
use std::io::Read;
use std::path::Path;

use anyhow::Result;

#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
#[repr(C)]
pub struct Digest {
    pub digest: [u8; 48],
}

pub fn digest(path: impl AsRef<Path>) -> Result<Digest> {
    let mut file = File::open(path)?;
    let mut data = Vec::new();
    file.read_to_end(&mut data)?;

    let md5 = md5::compute(&data).0;

    let sha256b = hex::decode(sha256::digest(&data))?;
    let mut sha256b_iter = sha256b.iter();
    let mut sha256 = [0u8; 32];
    sha256.fill_with(|| *sha256b_iter.next().unwrap());

    let digest = {
        let mut whole: [u8; 48] = [0; 48];
        let (one, two) = whole.split_at_mut(sha256.len());
        one.copy_from_slice(&sha256);
        two.copy_from_slice(&md5);
        whole
    };

    Ok(Digest { digest })
}
